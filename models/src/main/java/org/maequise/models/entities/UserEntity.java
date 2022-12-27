package org.maequise.models.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "USERS")
@Data
public class UserEntity {
    @Id
    @Column(name = "USER_ID")
    @GeneratedValue
    private Integer id;

    @Column(name ="USERNAME")
    private String username;

    @Column(name = "EMAIL")
    private String email;
}
