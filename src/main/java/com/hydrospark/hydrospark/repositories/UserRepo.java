package com.hydrospark.hydrospark.repositories;

import com.hydrospark.hydrospark.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<User,Integer> {


    @Query("select u from User u where u.email=?1 and u.password=?2")
    List<User> findAllByEmailAndPassword(String email, String password);

    @Query("select u from User u where u.email=?1 ")
    List<User> findByEmail(String email);

    @Query("select u from User u")
    List<User> findAll();

}
