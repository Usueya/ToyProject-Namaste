package org.spring.my.dao;

import java.util.List;
import java.util.Map;

import org.spring.my.dto.Ysales;

public interface YsalesDAO {
	public void salesAdd(Ysales ySales);
	public Ysales selectOne(int salenum);
	public  List<Ysales> selectListUser(String userid);
	public List<Ysales> selectListClass(int clsnum);
	public List<Map<String, Object>> selectList();
	public void saleUpdate(Ysales ySales);
	public void saleDelete(int salenum);
}
