package com.gop.country;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.gop.mode.vo.PageModel;
import com.gte.countryarea.service.CountryAreaService;
import com.gte.domain.CountryArea;

@RestController
@RequestMapping("/country")
public class CountryController {
	private static final Logger log = LoggerFactory.getLogger(CountryController.class);

	@Autowired
	private CountryAreaService countryAreaService;
	
	@Autowired
	private Gson gson;

	/**
	 * 查询国家地区数据
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@ResponseBody
	public PageModel<CountryArea> areaList() {
		return countryAreaService.getList();
	}

	/**
	 * 新增国家地区
	 * @param countryArea
	 */
	@RequestMapping(value = "/addCountry", method = RequestMethod.POST)
	@ResponseBody
	public void addCountry(@RequestBody CountryArea countryArea) {
		log.info("新增国家地区数据={}",gson.toJson(countryArea));
		countryAreaService.addCountry(countryArea);
	}
	
	
	/**
	 * 更新国家状态
	 * @param countryArea
	 */
	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public void updateStatus(@RequestBody CountryArea countryArea) {
		log.info("新增国家地区数据={}",gson.toJson(countryArea));
		countryAreaService.updateStatus(countryArea);
	}

}
