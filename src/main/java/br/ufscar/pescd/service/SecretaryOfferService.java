package br.ufscar.pescd.service;

import br.ufscar.pescd.entity.OfferStudentStatusLog;
import br.ufscar.pescd.repository.OfferStudentStatusLogRepository;
import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferForm;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.ImportStudentsResponse;
import br.ufscar.pescd.dto.SecretaryCloseOfferPreviewResponse;
import br.ufscar.pescd.dto.StudentForm;
import br.ufscar.pescd.dto.UserSummaryResponse;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.OfferStatus;
import br.ufscar.pescd.enums.StudentOfferStatus;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.repository.OfferRepository;
import br.ufscar.pescd.repository.OfferStudentRepository;
import br.ufscar.pescd.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import br.ufscar.pescd.entity.StatusChangeLog;
import br.ufscar.pescd.repository.StatusChangeLogRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecretaryOfferService {

    private static final String CLOSE_OFFER_INSTRUCTIONS =
            "Confira os dados da oferta antes de confirmar o encerramento. "
                    + "Ao confirmar, os créditos serão atribuídos aos alunos e a oferta será concluída.";

    private final OfferRepository offerRepository;
    private final OfferStudentRepository offerStudentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StatusChangeLogRepository statusChangeLogRepository;
    private final OfferApiMapper offerApiMapper;

    public SecretaryOfferService(
            OfferRepository offerRepository,
            OfferStudentRepository offerStudentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            StatusChangeLogRepository statusChangeLogRepository,
            OfferApiMapper offerApiMapper
    ) {
        this.offerRepository = offerRepository;
        this.offerStudentRepository = offerStudentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.statusChangeLogRepository = statusChangeLogRepository;
        this.offerApiMapper = offerApiMapper;
    }

    @Transactional(readOnly = true)
    public List<Offer> listOffers() {
        return offerRepository.findAllByOrderBySemesterDesc();
    }

    @Transactional(readOnly = true)
    public List<OfferSummaryResponse> listOffersForApi() {
        return listOffers().stream()
                .map(offerApiMapper::toOfferSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Offer getOffer(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<OfferStudent> getOfferStudents(Long offerId) {
        Offer offer = getOffer(offerId);
        return offerStudentRepository.findByOffer(offer);
    }

    @Transactional(readOnly = true)
    public OfferDetailsResponse getOfferDetailsForApi(Long offerId) {
        Offer offer = getOffer(offerId);
        return offerApiMapper.toOfferDetails(offer, offerStudentRepository.findByOffer(offer));
    }

    @Transactional(readOnly = true)
    public OfferStudentDetailsResponse getStudentDetailsForApi(Long offerId, Long offerStudentId) {
        Offer offer = getOffer(offerId);
        OfferStudent offerStudent = getOfferStudent(offerStudentId);
        if (!offerStudent.getOffer().getId().equals(offer.getId())) {
            throw new IllegalArgumentException("O aluno informado não pertence a esta oferta.");
        }
        return offerApiMapper.toOfferStudentDetails(offerStudent);
    }

    @Transactional(readOnly = true)
    public SecretaryCloseOfferPreviewResponse getCloseOfferPreviewForApi(Long offerId) {
        Offer offer = getOffer(offerId);
        return new SecretaryCloseOfferPreviewResponse(
                offerApiMapper.toOfferSummary(offer),
                offer.getStatus() == OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO,
                CLOSE_OFFER_INSTRUCTIONS
        );
    }

    @Transactional(readOnly = true)
    public List<StatusChangeLog> getStudentLogs(Long offerStudentId) {

        OfferStudent offerStudent = getOfferStudent(offerStudentId);

        return statusChangeLogRepository
                .findByOfferStudentOrderByChangedAtAsc(offerStudent);
    }

    @Transactional(readOnly = true)
    public List<User> listProfessors() {
        return userRepository.findByRole(UserRole.PROFESSOR);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listProfessorsForApi() {
        return listProfessors().stream()
                .map(offerApiMapper::toUserSummary)
                .toList();
    }

    @Transactional
    public OfferSummaryResponse createOfferForApi(OfferForm form, String creatorUsername) {
        if (form.getStartDate() != null
                && form.getEndDate() != null
                && !form.getEndDate().isAfter(form.getStartDate())) {
            throw new IllegalArgumentException("A data de fim deve ser depois da data de início.");
        }
        return offerApiMapper.toOfferSummary(createOffer(form, creatorUsername));
    }

    @Transactional
    public OfferDetailsResponse addStudentForApi(Long offerId, StudentForm form) {
        addStudent(offerId, form);
        return getOfferDetailsForApi(offerId);
    }

    @Transactional
    public ImportStudentsResponse importStudentsFromCsvForApi(Long offerId, MultipartFile file) {
        int enrolled = importStudentsFromCsv(offerId, file);
        return new ImportStudentsResponse(enrolled, getOfferDetailsForApi(offerId));
    }

    @Transactional
    public Offer createOffer(OfferForm form, String creatorUsername) {
        User professor = userRepository.findById(form.getResponsibleProfessorId())
                .filter(user -> user.getRole() == UserRole.PROFESSOR)
                .orElseThrow(() -> new IllegalArgumentException("Professor responsável inválido."));

        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuário criador não encontrado."));

        Offer offer = new Offer();
        String name = form.getName() != null ? form.getName().trim() : "";
        offer.setName(name.isBlank() ? "Oferta PESCD " + form.getSemester() : name);
        offer.setSemester(form.getSemester());
        offer.setStartDate(form.getStartDate());
        offer.setEndDate(form.getEndDate());
        offer.setStatus(OfferStatus.EM_ANDAMENTO);
        offer.setResponsibleProfessor(professor);
        offer.setCreatedBy(creator);

        return offerRepository.save(offer);
    }

    @Transactional
    public void addStudent(Long offerId, StudentForm form) {
        Offer offer = getOffer(offerId);
        ensureOfferIsEditable(offer);

        User student = userRepository.findByEmail(form.getEmail()).orElse(null);

        if (student == null) {
            if (userRepository.existsByUsername(form.getEmail())) {
                throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
            }
            student = new User();
            student.setFullName(form.getFullName());
            student.setEmail(form.getEmail());
            student.setUsername(form.getEmail());
            student.setPassword(passwordEncoder.encode(form.getRa()));
            student.setRole(UserRole.ALUNO);
            student.setEnabled(true);
            student = userRepository.save(student);
        } else if (student.getRole() != UserRole.ALUNO) {
            throw new IllegalArgumentException("O e-mail informado pertence a um usuário que não é aluno.");
        }

        if (!enroll(offer, student)) {
            throw new IllegalArgumentException("Este aluno já está inscrito nesta oferta.");
        }
    }

    @Transactional
    public int importStudentsFromCsv(Long offerId, MultipartFile file) {
        Offer offer = getOffer(offerId);
        ensureOfferIsEditable(offer);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo CSV.");
        }

        int enrolled = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine();
            if (line == null) {
                throw new IllegalArgumentException("O arquivo CSV está vazio.");
            }

            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = line.split(",", -1);
                if (columns.length < 3) {
                    throw new IllegalArgumentException(
                            "Linha " + lineNumber + " inválida: esperado RA,NOME_COMPLETO,EMAIL.");
                }

                String ra = columns[0].trim();
                String fullName = columns[1].trim();
                String email = columns[2].trim();

                if (ra.isEmpty() || fullName.isEmpty() || email.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Linha " + lineNumber + " possui campos vazios.");
                }

                User student = userRepository.findByEmail(email).orElse(null);
                if (student == null) {
                    student = new User();
                    student.setFullName(fullName);
                    student.setEmail(email);
                    student.setUsername(email);
                    student.setPassword(passwordEncoder.encode(ra));
                    student.setRole(UserRole.ALUNO);
                    student.setEnabled(true);
                    student = userRepository.save(student);
                }

                if (enroll(offer, student)) {
                    enrolled++;
                }
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo CSV.", ex);
        }

        return enrolled;
    }

    @Transactional
    public void closeOffer(Long id) {
        closeOffer(id, null);
    }

    @Transactional
    public OfferSummaryResponse closeOfferForApi(Long id, String secretaryUsername) {
        return offerApiMapper.toOfferSummary(closeOffer(id, secretaryUsername));
    }

    @Transactional
    public Offer closeOffer(Long id, String secretaryUsername) {
        Offer offer = getOffer(id);

        if (offer.getStatus() != OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO) {
            throw new IllegalArgumentException(
                    "A oferta ainda não está aguardando encerramento do secretário."
            );
        }

        offer.setStatus(OfferStatus.CONCLUIDA);
        offer.setClosedAt(LocalDateTime.now());
        if (secretaryUsername != null) {
            User secretary = userRepository.findByUsername(secretaryUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Secretário autenticado não encontrado."));
            offer.setClosedBy(secretary);
        }

        return offerRepository.save(offer);
    }



    @Transactional(readOnly = true)
    public OfferStudent getOfferStudent(Long id) {
        return offerStudentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Aluno da oferta não encontrado."));
    }

    private boolean enroll(Offer offer, User student) {
        if (offerStudentRepository.existsByOfferAndStudent(offer, student)) {
            return false;
        }
        OfferStudent offerStudent = new OfferStudent();
        offerStudent.setOffer(offer);
        offerStudent.setStudent(student);
        offerStudent.setStatus(StudentOfferStatus.NAO_ENVIADO);
        offerStudentRepository.save(offerStudent);
        return true;
    }

    private void ensureOfferIsEditable(Offer offer) {
        if (offer.getStatus() == OfferStatus.CONCLUIDA) {
            throw new IllegalArgumentException("A oferta está concluída e não pode mais ser alterada.");
        }
    }
}
