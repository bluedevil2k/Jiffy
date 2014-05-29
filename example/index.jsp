<%@ include file="_jiffy.jsp" %>

<%
	Security.validateAccess(request, Roles.ALL);
%>

<h2>Welcome to Jiffy!</h2>