package com.example.findHataAuth.repositories;

import com.example.findHataAuth.entities.MyUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<MyUser, Integer> {

    MyUser findUserByLogin(String login);

    MyUser save(MyUser user);
}
