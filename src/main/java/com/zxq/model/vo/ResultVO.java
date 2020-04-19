package com.zxq.model.vo;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;


/**
 * @author zxq
 */
@Data
public class ResultVO<T> implements Serializable {

	/**
	 * 请求状态码
	 */
	private Integer status;

	/**
	 * 请求状态描述
	 */
	private String message;

	/**
	 * 响应数据
	 */
	private T data;

	/**
	 * 请求成功
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> success() {
		ResultVO<T> vo = new ResultVO<>();
		vo.setStatus(HttpStatus.OK.value());
		vo.setMessage(HttpStatus.OK.getReasonPhrase());
		return vo;
	}

	/**
	 * 请求成功，指定响应提示
	 * @param message
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> success(String message) {
		ResultVO<T> vo = success();
		vo.setMessage(message);
		return vo;
	}

	/**
	 * 请求成功，指定响应数据
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> success(T t) {
		ResultVO<T> vo = success();
		vo.setData(t);
		return vo;
	}

	/**
	 * 请求成功，指定响应提示、响应数据
	 * @param message
	 * @param t
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> success(String message, T t) {
		ResultVO<T> vo = success();
		vo.setMessage(message);
		vo.setData(t);
		return vo;
	}

	/**
	 * 请求失败
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> failure() {
		ResultVO<T> vo = new ResultVO<>();
		vo.setStatus(HttpStatus.BAD_REQUEST.value());
		vo.setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
		return vo;
	}

	/**
	 * 请求失败，指定响应提示
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> failure(String message) {
		ResultVO<T> vo = failure();
		vo.setMessage(message);
		return vo;
	}

	/**
	 * 请求失败，指定响应数据
	 * @param <T>
	 * @return
	 */
	public static <T> ResultVO<T> failure(T t) {
		ResultVO<T> vo = failure();
		vo.setData(t);
		return vo;
	}

}
