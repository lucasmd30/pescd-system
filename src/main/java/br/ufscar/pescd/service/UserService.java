package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAllByOrderByFullNameAsc();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));
    }

    public UserForm toForm(User user) {
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setUsername(user.getUsername());
        form.setRole(user.getRole());
        return form;
    }

    public boolean validateBusinessRules(
            UserForm form,
            BindingResult bindingResult,
            boolean editing
    ) {
        Long currentId = editing ? form.getId() : null;

        if (currentId == null) {
            if (userRepository.existsByEmail(form.getEmail())) {
                bindingResult.rejectValue("email", "duplicate", "Já existe um usuário com este e-mail.");
            }
            if (userRepository.existsByUsername(form.getUsername())) {
                bindingResult.rejectValue("username", "duplicate", "Já existe um usuário com este nome de usuário.");
            }
        } else {
            if (userRepository.existsByEmailAndIdNot(form.getEmail(), currentId)) {
                bindingResult.rejectValue("email", "duplicate", "Já existe um usuário com este e-mail.");
            }
            if (userRepository.existsByUsernameAndIdNot(form.getUsername(), currentId)) {
                bindingResult.rejectValue("username", "duplicate", "Já existe um usuário com este nome de usuário.");
            }
        }

        if (!editing && (form.getPassword() == null || form.getPassword().isBlank())) {
            bindingResult.rejectValue("password", "required", "A senha é obrigatória.");
        }

        if (form.getPassword() != null && !form.getPassword().isBlank() && form.getPassword().length() < 6) {
            bindingResult.rejectValue("password", "size", "A senha deve ter pelo menos 6 caracteres.");
        }

        return !bindingResult.hasErrors();
    }

    @Transactional
    public void create(UserForm form) {
        User user = new User();
        applyForm(user, form, true);
        userRepository.save(user);
    }

    @Transactional
    public void update(UserForm form) {
        User user = findById(form.getId());
        applyForm(user, form, false);
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id, String loggedUsername) {
        User user = findById(id);

        if (user.getUsername().equals(loggedUsername)) {
            throw new IllegalArgumentException("Você não pode remover seu próprio usuário.");
        }

        userRepository.delete(user);
    }

    private void applyForm(User user, UserForm form, boolean creating) {
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setUsername(form.getUsername());
        user.setRole(form.getRole());
        user.setEnabled(true);

        if (creating || (form.getPassword() != null && !form.getPassword().isBlank())) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
    }
}
