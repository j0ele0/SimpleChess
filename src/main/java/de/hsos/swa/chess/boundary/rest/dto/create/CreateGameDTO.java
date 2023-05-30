package de.hsos.swa.chess.boundary.rest.dto.create;


import io.smallrye.common.constraint.Nullable;
import javax.validation.constraints.Size;

public class CreateGameDTO {
    @Nullable
    @Size(message = "Your GameName should be shorter than 20 chars.", max = 20)
    public String gameName;
}
