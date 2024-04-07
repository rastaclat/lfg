package me.bramar.task.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import me.bramar.task.enums.ResultCodeEnum;

@Data
@NoArgsConstructor
public class Ret<T> {
    private static final long serialVersionUID = 411731814484355577L;
    /**
     * 状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 相关数据
     */
    private T data;

    public String toString() {
        return "Ret(code=" + this.getCode() + ", msg=" + this.getMsg() + ", data=" + this.getData() + ")";
    }

    /**
     * 构造器 自定义响应码与提示信息
     *
     * @param code    响应码
     * @param message 提示信息
     */
    private Ret(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    /**
     * 构造器 自定义响应码、提示信息、数据
     *
     * @param code    响应码
     * @param message 提示信息
     * @param data    返回数据
     */
    private Ret(int code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功构造器  无返回数据
     */
    public static <T> Ret<T> success() {
        return new Ret<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage());
    }

    /**
     * 成功构造器 自定义提示信息 无返回数据
     *
     * @param message 提示信息
     */
    public static <T> Ret<T> success(String message) {
        return new Ret<>(ResultCodeEnum.SUCCESS.getCode(), message);
    }

    /**
     * 成功构造器  有返回数据
     */
    public static <T> Ret<T> success(T data) {
        return new Ret<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage(), data);
    }

    /**
     * 失败构造器  无返回数据
     */
    public static <T> Ret<T> fail() {
        return new Ret<>(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage());
    }

    /**
     * 失败构造器 自定义提示信息 无返回数据
     *
     * @param message 提示信息
     */
    public static <T> Ret<T> fail(String message) {
        return new Ret<>(ResultCodeEnum.FAIL.getCode(), message);
    }

    /**
     * 失败构造器  有返回数据
     */
    public static <T> Ret<T> fail(T data) {
        return new Ret<>(ResultCodeEnum.FAIL.getCode(), ResultCodeEnum.FAIL.getMessage(), data);
    }

}
