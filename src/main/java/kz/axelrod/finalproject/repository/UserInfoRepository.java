package kz.axelrod.finalproject.repository;

import kz.axelrod.finalproject.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    Optional<UserInfo> findByEmail(String email);

    Optional<UserInfo> findByUsername(String username);
}
