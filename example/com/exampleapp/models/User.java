package com.exampleapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jiffy.models.JiffyModel;
import org.jiffy.server.db.DB;
import org.jiffy.server.db.DBResult;
import org.jiffy.server.db.annotations.DBColumn;
import org.jiffy.server.db.annotations.DBTable;

@DBTable
public class User extends JiffyModel implements Serializable
{	
	@DBColumn
	public String userName;
	@DBColumn
	public String role;
	@DBColumn
	public String firstName;
	@DBColumn
	public String lastName;
	@DBColumn
	public String email;
	@DBColumn
	public String company;
	@DBColumn
	public String address1;
	@DBColumn
	public String address2;
	@DBColumn
	public String city;
	@DBColumn
	public String state;
	@DBColumn
	public String postalCode;
	@DBColumn
	public String phonePrimary;
	@DBColumn
	public String phoneCell;
	@DBColumn
	public String phoneFax;
	@DBColumn
	public int failedAttempts;
	@DBColumn
	public String password;
	@DBColumn
	public boolean forcePwChange;
	@DBColumn
	public boolean isFrozen = false;
	@DBColumn
	public boolean isDisabled = false;
	@DBColumn
	public String custom1;
	@DBColumn
	public String custom2;
	@DBColumn
	public String custom3;
	@DBColumn
	public String custom4;
	@DBColumn
	public String custom5;
	
	
	/* An example of a 1-to-many database relation
	@DBHasMany
	public ItemList items;
	*/
	
	/* An example of a 1-to-1 database relation	
	@DBHasOne
	public Item item;
	*/
			
	public static User lookup(long id) throws Exception
	{
		return DB.selectOne(User.class, "WHERE @id@=?", id);
	}

	public static UserList lookup(String userRole) throws Exception
	{
		return DB.selectAll(UserList.class, "WHERE @role@=? ORDER BY @userName@", userRole);
	}

	public static UserList lookup() throws Exception
	{
		return DB.selectAll(UserList.class, "ORDER BY @userName@");
	}
	
	public static User lookupForLogin(String username) throws Exception
	{
		return DB.selectOne(User.class, "WHERE @userName@=? AND @isFrozen@=false", username);
	}
	
	public static void markUserAsLoggedIn(String username) throws Exception
	{
		DB.update(User.class, "UPDATE @table@ SET @lastLogonTs@=NOW(), @failedAttempts@=0 WHERE @userName@=?", username);
	}
	
	public static void incrementFailedAttempts(String username, int failedAttempts) throws Exception
	{
        String updateSql = "UPDATE @table@ SET @failedAttempts@=? WHERE @userName@=?";
        DB.update(User.class, updateSql, failedAttempts, username);
	}
	
	public static void freezeUser(String username) throws Exception
	{
		String failSql = "UPDATE @table@ SET @isFrozen@=true, @failedAttempts@=0 WHERE @userName@=?";
        DB.update(User.class, failSql, username);
	}
	
	public static List<Long> getAllUserIDs(String userRole) throws Exception
	{
		String sql = "SELECT DISTINCT id FROM user WHERE role=? ORDER BY user_name ASC";
		DBResult rs = DB.select(sql, userRole);
		return rs.getAllRows("id");
	}

	public static List<Long> getAllUserIDs(List<String> userRoles) throws Exception
	{
		List<Long> toReturn = new ArrayList<Long>();
		for (Iterator<String> iter = userRoles.iterator(); iter.hasNext();)
		{
			toReturn.addAll(getAllUserIDs(iter.next()));
		}
		return toReturn;
	}
	
	public static void delete(int id) throws Exception
	{
		DB.update(User.class, "DELETE FROM @table@ WHERE @id@=?", id);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPhonePrimary() {
		return phonePrimary;
	}

	public void setPhonePrimary(String phonePrimary) {
		this.phonePrimary = phonePrimary;
	}

	public String getPhoneCell() {
		return phoneCell;
	}

	public void setPhoneCell(String phoneCell) {
		this.phoneCell = phoneCell;
	}

	public String getPhoneFax() {
		return phoneFax;
	}

	public void setPhoneFax(String phoneFax) {
		this.phoneFax = phoneFax;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isForcePwChange() {
		return forcePwChange;
	}

	public void setForcePwChange(boolean forcePwChange) {
		this.forcePwChange = forcePwChange;
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	public void setFrozen(boolean isFrozen) {
		this.isFrozen = isFrozen;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public String getCustom1() {
		return custom1;
	}

	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}

	public String getCustom2() {
		return custom2;
	}

	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}

	public String getCustom3() {
		return custom3;
	}

	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}

	public String getCustom4() {
		return custom4;
	}

	public void setCustom4(String custom4) {
		this.custom4 = custom4;
	}

	public String getCustom5() {
		return custom5;
	}

	public void setCustom5(String custom5) {
		this.custom5 = custom5;
	}
	
	
}
