package br.ufscar.pescd.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Serviço de armazenamento de arquivos no SeaweedFS.
 *
 * Substitui o antigo armazenamento de binários (bytea) no PostgreSQL. Cada
 * arquivo passa a ser guardado no SeaweedFS e no banco relacional guardamos
 * apenas o identificador retornado (o "fid", ex.: "3,01637037d6").
 *
 * Segue o fluxo do quick start do SeaweedFS (master + volume server):
 *   1. GET  {master}/dir/assign            -> reserva um fid e indica o volume
 *   2. POST http://{volume}/{fid}          -> grava o arquivo (multipart)
 *   3. GET  {master}/dir/lookup?volumeId=  -> descobre o volume para leitura
 *   4. GET  http://{volume}/{fid}          -> lê o arquivo
 */
@Service
public class SeaweedFsStorageService {

    private final RestClient restClient = RestClient.create();
    private final String masterUrl;

    public SeaweedFsStorageService(
            @Value("${seaweedfs.master-url:http://localhost:9333}") String masterUrl
    ) {
        this.masterUrl = masterUrl.endsWith("/")
                ? masterUrl.substring(0, masterUrl.length() - 1)
                : masterUrl;
    }

    /**
     * Grava o conteúdo no SeaweedFS e devolve o fid a ser persistido no banco.
     */
    public String store(byte[] content, String fileName, String contentType) {
        AssignResponse assign = restClient.get()
                .uri(masterUrl + "/dir/assign")
                .retrieve()
                .body(AssignResponse.class);

        if (assign == null || assign.fid() == null || assign.url() == null) {
            throw new IllegalStateException("SeaweedFS não retornou um fid válido em /dir/assign.");
        }

        String fid = assign.fid();
        String volumeUrl = assign.url();

        String safeName = (fileName != null && !fileName.isBlank()) ? fileName : fid;
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return safeName;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", resource);

        restClient.post()
                .uri("http://" + volumeUrl + "/" + fid)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        return fid;
    }

    /**
     * Lê o conteúdo de um arquivo previamente armazenado, a partir do seu fid.
     */
    public byte[] read(String fid) {
        if (fid == null || fid.isBlank()) {
            throw new IllegalArgumentException("Identificador de arquivo (fid) ausente.");
        }
        String volumeUrl = lookupVolumeUrl(fid);
        return restClient.get()
                .uri("http://" + volumeUrl + "/" + fid)
                .retrieve()
                .body(byte[].class);
    }

    /**
     * Remove um arquivo do SeaweedFS (best-effort, usado ao substituir envios).
     */
    public void delete(String fid) {
        if (fid == null || fid.isBlank()) {
            return;
        }
        try {
            String volumeUrl = lookupVolumeUrl(fid);
            restClient.delete()
                    .uri("http://" + volumeUrl + "/" + fid)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ignored) {
            // Remoção é best-effort: se falhar, o arquivo antigo apenas fica órfão.
        }
    }

    private String lookupVolumeUrl(String fid) {
        String volumeId = fid.contains(",") ? fid.substring(0, fid.indexOf(',')) : fid;
        LookupResponse lookup = restClient.get()
                .uri(masterUrl + "/dir/lookup?volumeId=" + volumeId)
                .retrieve()
                .body(LookupResponse.class);

        if (lookup == null || lookup.locations() == null || lookup.locations().isEmpty()) {
            throw new IllegalStateException("SeaweedFS não localizou o volume do arquivo " + fid + ".");
        }
        return lookup.locations().get(0).url();
    }

    // Respostas JSON do SeaweedFS (campos não usados são ignorados pelo Jackson).
    private record AssignResponse(String fid, String url, String publicUrl) {
    }

    private record LookupResponse(String volumeId, List<Location> locations) {
    }

    private record Location(String url, String publicUrl) {
    }
}
