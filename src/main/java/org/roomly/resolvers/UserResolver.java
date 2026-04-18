package org.roomly.resolvers;


import lombok.RequiredArgsConstructor;
import org.roomly.dto.UserDTO;
import org.roomly.services.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserService userService;
    
    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public UserDTO joinHousehold (
      @Argument String nickname,
      @Argument String avatarName,
      @Argument String avatarColorName,
      @Argument String joinCode
    ) {
        return userService.joinHousehold(nickname, avatarName, avatarColorName, joinCode);
    }
}
