var app = app || {};
app.server = "http://localhost:9000";
app.generateShortUrl = function() {
	var shortUrlAnchor = $("#shortUrl");
	shortUrlAnchor.text("");

	var fullUrl = $("#fullUrl").val();
	$.ajax({
		method : "POST",
		url : app.server + "/url",
		data : fullUrl,
		contentType : "text/plain",
		success : function(shortUrlPath) {
			var shortUrl = app.server + shortUrlPath
			shortUrlAnchor.attr("href", shortUrl);
			shortUrlAnchor.text(shortUrl);
		},
		error : function() {
			alert("error; check console");
		}
	});
}
