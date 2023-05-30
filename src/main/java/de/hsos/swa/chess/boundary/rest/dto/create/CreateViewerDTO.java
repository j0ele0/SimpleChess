package de.hsos.swa.chess.boundary.rest.dto.create;

import javax.validation.constraints.Size;

public class CreateViewerDTO {

    @Size(message = "The joinToken should have a length between 5-20.", min = 5, max = 20)
    public String joinToken;
}
