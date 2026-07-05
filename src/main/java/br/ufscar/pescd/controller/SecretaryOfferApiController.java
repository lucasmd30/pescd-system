package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.ImportStudentsResponse;
import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferForm;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.SecretaryCloseOfferPreviewResponse;
import br.ufscar.pescd.dto.SecretaryCloseOfferRequest;
import br.ufscar.pescd.dto.StudentForm;
import br.ufscar.pescd.dto.UserSummaryResponse;
import br.ufscar.pescd.service.SecretaryOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/secretary/offers")
public class SecretaryOfferApiController {

    private final SecretaryOfferService secretaryOfferService;

    public SecretaryOfferApiController(SecretaryOfferService secretaryOfferService) {
        this.secretaryOfferService = secretaryOfferService;
    }

    @GetMapping
    public List<OfferSummaryResponse> listOffers() {
        return secretaryOfferService.listOffersForApi();
    }

    // S.01 - lista de professores para escolha do responsável no formulário de criação.
    @GetMapping("/professors")
    public List<UserSummaryResponse> listProfessors() {
        return secretaryOfferService.listProfessorsForApi();
    }

    // S.01 - criação de uma nova oferta.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferSummaryResponse createOffer(
            @Valid @RequestBody OfferForm form,
            Authentication authentication
    ) {
        return secretaryOfferService.createOfferForApi(form, authentication.getName());
    }

    @GetMapping("/{offerId}")
    public OfferDetailsResponse offerDetails(@PathVariable Long offerId) {
        return secretaryOfferService.getOfferDetailsForApi(offerId);
    }

    // S.02 - inclusão manual de um aluno na oferta.
    @PostMapping("/{offerId}/students")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferDetailsResponse addStudent(
            @PathVariable Long offerId,
            @Valid @RequestBody StudentForm form
    ) {
        return secretaryOfferService.addStudentForApi(offerId, form);
    }

    // S.02 - importação de alunos via arquivo CSV (RA,NOME_COMPLETO,EMAIL).
    @PostMapping("/{offerId}/students/import")
    public ImportStudentsResponse importStudents(
            @PathVariable Long offerId,
            @RequestParam("file") MultipartFile file
    ) {
        return secretaryOfferService.importStudentsFromCsvForApi(offerId, file);
    }

    @GetMapping("/{offerId}/students/{offerStudentId}")
    public OfferStudentDetailsResponse studentDetails(
            @PathVariable Long offerId,
            @PathVariable Long offerStudentId
    ) {
        return secretaryOfferService.getStudentDetailsForApi(offerId, offerStudentId);
    }

    @GetMapping("/{offerId}/close")
    public SecretaryCloseOfferPreviewResponse closePreview(@PathVariable Long offerId) {
        return secretaryOfferService.getCloseOfferPreviewForApi(offerId);
    }

    @PostMapping("/{offerId}/close")
    public OfferSummaryResponse closeOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody SecretaryCloseOfferRequest request,
            Authentication authentication
    ) {
        return secretaryOfferService.closeOfferForApi(offerId, authentication.getName());
    }
}
