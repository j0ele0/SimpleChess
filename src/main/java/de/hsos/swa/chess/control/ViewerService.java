package de.hsos.swa.chess.control;

import de.hsos.swa.chess.boundary.rest.dto.create.CreateViewerDTO;
import de.hsos.swa.chess.boundary.rest.dto.response.ViewerResponseDTO;

import java.util.Optional;

public interface ViewerService {
    public Optional<ViewerResponseDTO> addViewerToGame(CreateViewerDTO newViewer, String gameName);
}
