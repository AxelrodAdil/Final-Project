//package kz.axelrod.finalproject.config.security;
//
//import kz.axelrod.finalproject.model.UserInfo;
//import kz.axelrod.finalproject.repository.UserInfoRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    @Autowired
//    private UserInfoRepository userRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        var userInfo = userRepository.findByUsername(username);
//        if (userInfo.isEmpty()) throw new UsernameNotFoundException("User not found with username: " + username);
//        return new User(userInfo.get().getEmail(), userInfo.get().getPassword(),
//                getAuthority(userInfo.get()));
//    }
//
//    private List<SimpleGrantedAuthority> getAuthority(UserInfo userInfo) {
//        return userInfo.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
//                .collect(Collectors.toList());
//    }
//}
