package com.project.makecake.repository;

import com.project.makecake.enums.NotiType;
import com.project.makecake.domain.noti.Noti;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotiRepository extends JpaRepository<Noti,Long> {

    Noti findByType(NotiType comment);

}
