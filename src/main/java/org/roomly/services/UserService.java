package org.roomly.services;

import lombok.RequiredArgsConstructor;
import org.roomly.dto.AvatarDTO;
import org.roomly.dto.UserDTO;
import org.roomly.entities.Household;
import org.roomly.entities.User;
import org.roomly.repositories.UserRepository;
import org.roomly.security.authentication.entities.Account;
import org.roomly.security.authentication.services.AuthenticationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ColorsService colorsService;
    private final HouseholdService householdService;
    private final AuthenticationService authenticationService;
    
    public UserDTO joinHousehold (String nickname, String avatarName, String avatarColorName, String joinCode) {
        Account account = getCurrentlyAuthenticatedAccount();
        Household household = householdService.getHouseHoldByJoinCode(joinCode);
        
        validateJoinHousehold(account, household, nickname, avatarName, avatarColorName);
        
        User savedUser = createAndSaveUser(nickname, avatarName, avatarColorName, account, household);
        
        return buildUserDTO(savedUser);
    }
    
    private Account getCurrentlyAuthenticatedAccount () {
        String accountId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
          .map(Principal::getName)
          .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        
        return authenticationService.loadAccountById(accountId);
    }
    
    private void validateJoinHousehold(Account account, Household household, String nickname, String avatarName, String avatarColorName) {
        validateNotAlreadyMember(account, household);
        validateNicknameAvailability(household, nickname);
        validateHouseholdCapacity(household);
        validateAvatarCombinationAvailability(household, avatarName, avatarColorName);
        validateAvatarColor(avatarColorName);
        validateAvatarName(avatarName);
    }
    
    private void validateNotAlreadyMember(Account account, Household household) {
        if (userRepository.existsByAccountAndHousehold(account, household)) {
            throw new IllegalArgumentException("User is already a member of this household");
        }
    }
    
    private void validateNicknameAvailability(Household household, String nickname) {
        if (userRepository.existsByHouseholdAndNickname(household, nickname)) {
            throw new IllegalArgumentException("Nickname is already taken in this household");
        }
    }
    
    private void validateHouseholdCapacity(Household household) {
        if (userRepository.countByHousehold(household) >= household.getMembersLimit()) {
            throw new IllegalStateException("Household has reached its members limit");
        }
    }
    
    private void validateAvatarCombinationAvailability(Household household, String avatarName, String avatarColorName) {
        if (userRepository.existsByHouseholdAndAvatarNameAndAvatarColorName(household, avatarName, avatarColorName)) {
            throw new IllegalArgumentException("Avatar name or color is already taken in this household");
        }
    }
    
    private void validateAvatarColor(String avatarColorName) {
        if (!colorsService.isValidColor(avatarColorName)) {
            throw new IllegalArgumentException("Invalid avatar color: " + avatarColorName);
        }
    }
    
    private void validateAvatarName(String avatarName) {
        //TODO validate by checking if the avatar name exists in the catalog of available avatars. For now, just check if it's not empty.
        if (avatarName == null || avatarName.trim().isEmpty()) {
            throw new IllegalArgumentException("Avatar name cannot be empty");
        }
    }
    
    private User createAndSaveUser(String nickname, String avatarName, String avatarColorName, Account account, Household household) {
        return userRepository.save(
          new User()
            .setNickname(nickname)
            .setAccount(account)
            .setHousehold(household)
            .setAvatarName(avatarName)
            .setAvatarColorName(avatarColorName));
    }
    
    private UserDTO buildUserDTO(User user) {
        return new UserDTO(
          user.getNickname(),
          new AvatarDTO(
            user.getAvatarName(),
            user.getAvatarColorName(),
            colorsService.getHexByColor(user.getAvatarColorName())
          )
        );
    }
    
    
}
