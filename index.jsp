<%@ include file="_jiffy.jsp" %>

<%
	Security.validateAccess(request, User.ALL);
%>

<h2>Welcome to Jiffy!</h2>