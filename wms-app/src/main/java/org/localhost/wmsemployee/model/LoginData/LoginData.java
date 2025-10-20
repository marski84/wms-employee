package org.localhost.wmsemployee.model.LoginData;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LoginData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;


}


//{
//        "email": "roman.kowalski@example.com",
//        "password": null,
//        "connection": null,
//        "name": "Roman Kowalski",
//        "nickname": "Roman",
//        "username": "roman.kowalski",
//        "email_verified": true,
//        "user_metadata": {
//        "phoneNumber": "+48123456789",
//        "address": "ul. Przykładowa 123",
//        "city": "Warszawa",
//        "postalCode": "00-001",
//        "country": "Polska",
//        "roleId": "1",
//        "roleName": "EMPLOYEE",
//        "familyName": "Kowalski"
//        }
//        }