package com.project.makecake.domain.user;

import com.project.makecake.domain.Timestamped;
import com.project.makecake.enums.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class User  extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String password;

    @Column
    private String profileImgUrl;

    @Column
    private String profileImgName;

    @Column
    private String providerEmail;

    @Column
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @Column
    private String provider;

    @Column
    private String providerId;

    @Builder
    public User(
            String username,
            String nickname,
            String password,
            String profileImgUrl,
            String profileImgName,
            String providerEmail,
            UserRoleEnum role,
            String provider,
            String providerId
    ){
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.profileImgUrl = profileImgUrl;
        this.profileImgName = profileImgName;
        this.providerEmail = providerEmail;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }
}
