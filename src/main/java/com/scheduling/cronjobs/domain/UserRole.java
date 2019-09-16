package com.scheduling.cronjobs.domain;

import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class UserRole extends BaseEntity {

    /**
     * Наименование ролей
     */
    private String name;

    /**
     * Описание ролей
     */
    private String description;
}
