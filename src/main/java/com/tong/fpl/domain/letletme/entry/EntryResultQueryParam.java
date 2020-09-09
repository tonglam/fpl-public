package com.tong.fpl.domain.letletme.entry;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Valid
@Data
public class EntryResultQueryParam {

	@NotNull
	private int startGw;
	@NotNull
	private int endGw;
	@NotNull
	private int entry;
	private int page;
	private int limit;

}
