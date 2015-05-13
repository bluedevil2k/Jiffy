<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@page import="org.jiffy.server.security.*"%>
<%@page import="org.jiffy.util.*"%>
<%@page import="org.jiffy.server.*"%>
<%@page import="org.jiffy.models.*"%>

<%@page import="java.util.*"%>

<%
	JSPUtil.noCache(response);
	UserSession userSession = Sessions.getSession(request);
	Flash flash = Flash.retrieve(request);
%>
