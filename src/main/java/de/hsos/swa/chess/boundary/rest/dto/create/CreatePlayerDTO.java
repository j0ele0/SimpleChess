package de.hsos.swa.chess.boundary.rest.dto.create;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreatePlayerDTO {

    @Size(message = "The username should have a length between 3-20.", min = 3, max = 20)
    public String playerName;

    @Size(message = "The joinToken should have a length between 5-20.", min = 5, max = 20)
    public String joinToken;
}
