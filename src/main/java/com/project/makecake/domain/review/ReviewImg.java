package com.project.makecake.domain.review;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.dto.ImageInfoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class ReviewImg extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewImgId;

    @Column(nullable = false)
    private String imgUrl;

    @Column(nullable = false)
    private String imgName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID")
    private Review review;

    @Column(nullable = false)
    private String ThumbnailImgUrl;

    @Column(nullable = false)
    private String ThumbnailImgName;

    //생성자
    @Builder
    public ReviewImg(ImageInfoDto original,ImageInfoDto thumbnail, Review review){
        this.imgUrl = original.getUrl();
        this.imgName = original.getName();
        this.ThumbnailImgUrl = thumbnail.getUrl();
        this.ThumbnailImgName = thumbnail.getName();
        this.review= review;
    }

    public void addThumbnail(ImageInfoDto thumbnail) {
        this.ThumbnailImgUrl = thumbnail.getUrl();
        this.ThumbnailImgName = thumbnail.getName();
    }

}

