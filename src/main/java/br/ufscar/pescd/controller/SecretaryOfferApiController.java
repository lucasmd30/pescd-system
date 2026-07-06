package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Secretaria", description = "Operações REST da secretaria sobre ofertas e alunos.")
@SecurityRequirement(name = "sessionAuth")
public class SecretaryOfferApiController {

    private final SecretaryOfferService secretaryOfferService;

    public SecretaryOfferApiController(SecretaryOfferService secretaryOfferService) {
        this.secretaryOfferService = secretaryOfferService;
    }

    @GetMapping
    @Operation(summary = "Listar ofertas", description = "Retorna todas as ofertas gerenciadas pela secretaria.")
    public List<OfferSummaryResponse> listOffers() {
        return secretaryOfferService.listOffersForApi();
    }

    @GetMapping("/professors")
    @Operation(summary = "Listar professores", description = "Retorna os professores disponíveis para vinculação como responsáveis.")
    public List<UserSummaryResponse> listProfessors() {
        return secretaryOfferService.listProfessorsForApi();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar oferta", description = "Cria uma nova oferta no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oferta criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public OfferSummaryResponse createOffer(
            @Valid @RequestBody OfferForm form,
            Authentication authentication
    ) {
        return secretaryOfferService.createOfferForApi(form, authentication.getName());
    }

    @GetMapping("/{offerId}")
    @Operation(summary = "Detalhar oferta", description = "Retorna os dados completos de uma oferta específica.")
    public OfferDetailsResponse offerDetails(@PathVariable Long offerId) {
        return secretaryOfferService.getOfferDetailsForApi(offerId);
    }

    @PostMapping("/{offerId}/students")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adicionar aluno à oferta", description = "Adiciona manualmente um aluno em uma oferta existente.")
    public OfferDetailsResponse addStudent(
            @PathVariable Long offerId,
            @Valid @RequestBody StudentForm form
    ) {
        return secretaryOfferService.addStudentForApi(offerId, form);
    }

    @PostMapping("/{offerId}/students/import")
    @Operation(summary = "Importar alunos por CSV", description = "Importa alunos para uma oferta a partir de um arquivo CSV no formato RA,NOME_COMPLETO,EMAIL.")
    public ImportStudentsResponse importStudents(
            @PathVariable Long offerId,
            @RequestParam("file") MultipartFile file
    ) {
        return secretaryOfferService.importStudentsFromCsvForApi(offerId, file);
    }

    @GetMapping("/{offerId}/students/{offerStudentId}")
    @Operation(summary = "Detalhar vínculo aluno-oferta", description = "Retorna os dados detalhados de um aluno inscrito em uma oferta.")
    public OfferStudentDetailsResponse studentDetails(
            @PathVariable Long offerId,
            @PathVariable Long offerStudentId
    ) {
        return secretaryOfferService.getStudentDetailsForApi(offerId, offerStudentId);
    }

    @GetMapping("/{offerId}/close")
    @Operation(summary = "Prévia de encerramento", description = "Retorna o resumo necessário para o encerramento da oferta pela secretaria.")
    public SecretaryCloseOfferPreviewResponse closePreview(@PathVariable Long offerId) {
        return secretaryOfferService.getCloseOfferPreviewForApi(offerId);
    }

    @PostMapping("/{offerId}/close")
    @Operation(summary = "Encerrar oferta", description = "Efetiva o encerramento de uma oferta pela secretaria.")
    public OfferSummaryResponse closeOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody SecretaryCloseOfferRequest request,
            Authentication authentication
    ) {
        return secretaryOfferService.closeOfferForApi(offerId, authentication.getName());
    }
}
