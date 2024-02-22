package com.tong.fpl.domain.letletme.global;

import com.tong.fpl.constant.Constant;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Create by tong on 2020/8/18
 */
@Data
@Accessors(chain = true)
public class ResponseData<T> implements Serializable {

    private static final long serialVersionUID = 7028710860837708314L;

    private int code;
    private T data;
    private String message;
    private String timestamp;

    public static <T> ResponseData<T> success() {
        return success(null);
    }

    public static <T> ResponseData<T> success(T data) {
        return new ResponseData<T>()
                .setCode(HttpStatus.OK.value())
                .setMessage("success")
                .setData(data)
                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATETIME)));
    }

    public static <T> ResponseData<T> fail(int code, String msg) {
        return fail(code, msg, null);
    }

    public static <T> ResponseData<T> fail(int code, String msg, T data) {
        return new ResponseData<T>()
                .setCode(code)
                .setMessage(msg)
                .setData(data)
                .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATETIME)));
    }

}
