package com.project.makecake.service.order;

import com.project.makecake.dto.DesignResponseDto;
import com.project.makecake.dto.ImageInfoDto;
import com.project.makecake.enums.FolderName;
import com.project.makecake.exceptionhandler.CustomException;
import com.project.makecake.exceptionhandler.ErrorCode;
import com.project.makecake.domain.order.Design;
import com.project.makecake.domain.user.User;
import com.project.makecake.repository.DesignRepository;
import com.project.makecake.repository.UserOrdersRepository;
import com.project.makecake.security.UserDetailsImpl;
import com.project.makecake.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DesignService {

    private final DesignRepository designRepository;
    private final S3Service s3Service;
    private final UserOrdersRepository userOrdersRepository;

    // 도안 저장 메소드
    @Transactional
    public DesignResponseDto addDesign(UserDetailsImpl userDetails, MultipartFile img) throws IOException {

        User user = userDetails.getUser();

        ImageInfoDto imgInfo = s3Service.uploadImg(img, FolderName.DESIGN.name());

        Design design = Design.builder()
                .imgInfo(imgInfo)
                .user(user)
                .build();
        Design savedDesign = designRepository.save(design);

        return new DesignResponseDto(savedDesign);
    }


    // 도안 삭제 메소드
    @Transactional
    public void removeDesign(UserDetailsImpl userDetails, long designId) {

        Design foundDesign = designRepository.findById(designId)
                .orElseThrow(()->new CustomException(ErrorCode.DESIGN_NOT_FOUND));

        if (foundDesign.isPost()) {
            throw new CustomException(ErrorCode.DESIGN_ALREADY_POST);
        }

        if (!foundDesign.getUser().getUserId().equals(userDetails.getUser().getUserId())) {
            throw new CustomException(ErrorCode.NOT_DESIGN_OWNER);
        }

        userOrdersRepository.deleteByDesign(foundDesign);
        s3Service.deleteFile(foundDesign.getImgName());
        designRepository.deleteById(designId);
    }
}
