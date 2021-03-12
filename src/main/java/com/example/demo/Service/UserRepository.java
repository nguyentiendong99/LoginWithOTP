package com.example.demo.Service;

import com.example.demo.Entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    @Query("select u from User u where u.username = :username")
    User getUserByUsername(@Param("username") String username);

    @Query("select u from User u where u.email = ?1")
    User findByEmail(String email);

    @Query("update User u set u.failedAttempt = ?1 where u.username = ?2")
    @Modifying
    void updateFailedAttempt(int failedAttempt, String username);

    @Query("update User c set c.enabled = true where c.id = ?1")
    @Modifying
    void enable(Integer id);

    @Query("select c from User c where c.verificationCode = ?1")
    User findByVeritification(String code);


    User findByResetPasswordToken(String token);
}
