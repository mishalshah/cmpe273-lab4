$(":button").click(function() {
	var isbn = this.id;
	//this.disabled=true;
	var URL = "/library/v1/books/"+isbn+"?status=lost";
	alert('About to report lost on ISBN ' + isbn);
	$.ajax({
	    type: "PUT",
	    url: URL,
	    contentType: "application/json",
	    data: {"data": isbn+"?status=lost"},
	//	success: function () { success(data); }
	success: function() {
          window.location.reload();
	}

	});
	
});


$(function(){ 
	
	$('button').each(function(){
	if(this.value == "lost")
	{
		$(this).prop('disabled', true);
	}

	 });
});
