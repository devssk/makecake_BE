package com.project.makecake.domain.noti;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity
public class PersonalNoti extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long personalNotiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="receiveUserId")
    private User recieveUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="createUserId")
    private User createUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="notiId")
    private Noti noti;

    @Column
    private String redirectUrl;

    @Column(nullable = false)
    private boolean checked;

    @Builder
    public PersonalNoti(User recieveUser, User createUser, Noti noti, String redirectUrl) {
        this.recieveUser = recieveUser;
        this.createUser = createUser;
        this.noti = noti;
        this.redirectUrl = redirectUrl;
        this.checked = false;
    }

    // checked 변경 메소드
    public void editChecked() {
        this.checked=true;
    }

}
