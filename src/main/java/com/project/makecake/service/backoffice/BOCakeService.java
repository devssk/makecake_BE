package com.project.makecake.service.backoffice;

import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.enums.FolderName;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.product.Cake;
import com.project.makecake.domain.store.Store;
import com.project.makecake.repository.CakeLikeRepository;
import com.project.makecake.repository.CakeRepository;
import com.project.makecake.repository.StoreRepository;
import com.project.makecake.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BOCakeService {

    private final StoreRepository storeRepository;
    private final CakeRepository cakeRepository;
    private final CakeLikeRepository cakeLikeRepository;
    private final S3Service s3Service;

    // (관리자용) 가게별 케이크 사진 리스트 조회 메소드
    public List<Cake> GetCakeListAtBackoffice(long storeId) {

        Store foundStore = storeRepository.findById(storeId)
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));

        List<Cake> foundCakeList = cakeRepository.findAllByStore(foundStore);
        return foundCakeList;
    }

    // (관리자용) 케이크 사진 삭제 메소드
    @Transactional
    public long deleteCake(long cakeId) {

        Cake foundCake = cakeRepository.findById(cakeId)
                .orElseThrow(()->new CustomException(ErrorCode.CAKE_NOT_FOUND));

        cakeLikeRepository.deleteAllByCake(foundCake);
        cakeRepository.delete(foundCake);

        return foundCake.getCakeId();
    }

    // (관리자용) 케이크 사진 저장 메소드
    @Transactional
    public void addCakeList(long storeId, List<MultipartFile> imgFileList) throws IOException {

        Store foundStore = storeRepository.findById(storeId)
                .orElseThrow(()->new CustomException(ErrorCode.STORE_NOT_FOUND));

        if(imgFileList != null){
            for(MultipartFile imgFile : imgFileList){
                ImageInfoDto imgInfo = s3Service.uploadImg(imgFile, FolderName.Cake.name());
                Cake cake = Cake.builder()
                        .url(imgInfo.getUrl())
                        .store(foundStore)
                        .build();
                cakeRepository.save(cake);
            }
        }
    }
}
