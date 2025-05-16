package com.project.makecake.domain.order;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import com.project.makecake.dto.ImageInfoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Entity
public class Design extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long designId;

    @Column(nullable = false)
    private String imgUrl;

    @Column(nullable = false)
    private String imgName;

    @Column(nullable = false)
    private boolean post;

    @Column(nullable = false)
    private boolean orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="userId")
    private User user;

    @Builder
    public Design(ImageInfoDto imgInfo, User user) {
        this.imgUrl = imgInfo.getUrl();
        this.imgName = imgInfo.getName();
        this.post = false;
        this.orders = false;
        this.user = user;
    }

    // 도안 게시에 따라 postState 바꾸기
    public void editPostState(boolean postState) {
        this.post = postState;
    }

    // 도안 게시에 따라 orderState바꾸기
    public void editOrderState(boolean orderState) {
        this.orders = orderState;
    }
}
