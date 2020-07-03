package vn.candicode.payload.response.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class Tag implements Serializable {
    private String name;
    private Integer count;
}
