<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="expire" content="0" />
<title>PlexRBAC</title>
<script src="http://code.jquery.com/jquery-latest.js"></script>
<link rel="stylesheet"
	href="http://dev.jquery.com/view/trunk/plugins/autocomplete/jquery.autocomplete.css"
	type="text/css" />
<script type="text/javascript"
	src="http://dev.jquery.com/view/trunk/plugins/autocomplete/lib/jquery.bgiframe.min.js"></script>
<script type="text/javascript"
	src="http://dev.jquery.com/view/trunk/plugins/autocomplete/lib/jquery.dimensions.js"></script>
<script type="text/javascript"
	src="http://dev.jquery.com/view/trunk/plugins/autocomplete/jquery.autocomplete.js"></script>

<script type="text/javascript">
		$(document).ready( function() {
			$('#ajax_loading').hide();
			$(".search-cls").click( function() {
				search()
			});
			$(".explain-cls").click( function() {
				explain()
			});
			$(".top-cls").click( function() {
				top()
			});
		       if (false) { 
			$("#keywords").autocomplete("/api/search/autocomplete/efile_providers",
			{
				delay:10,
				minChars:2,
				matchSubset:1,
				matchContains:1,
				cacheLength:10,
				autoFill:false
                                });
                       }
		});
	
	
	//$("#search_form").submit( function() { search() }); $(".kw").change(function() {search()}); 
	function show(id) {
		var href = "/api/storage/efile_providers/" + id;
		$('#ajax_loading').show();
		jQuery.ajax( {
			type : "GET",
			url : href,
			dataType : "json",
			success : function(details) {
				$('#ajax_loading').hide();
				var textToInsert = '';
				for ( var name in details) {
					if (name.match(/latitude/) || name.match(/longitude/) || name.match(/indexDate/)) {
						continue;
					}
					var value = details[name];
					value = value.toString();
					if (value.length > 200) {
						value = value.substring(0, 200);
					}
					textToInsert += '<li>' + name + ': ' + value + '</li>\n';
				}
				var list = $('<ul/>');
				list.append(textToInsert);
				$('#details').empty();
				$('#details').append(list).show();
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				alert("Error getting details");
			}
		});
	}

	function similar(externalId, luceneId) {
		var href = "/api/search/similar/efile_providers?externalId=" + externalId + "&luceneId=" + luceneId + "&detailedResults=true";
		doSearch(href, '#details');
	}

	function search() {
		var kw = $("input#keywords").val();
		var zipCode = ''; //$("input#zipCode").val();
		var radius = ''; // $("#radius").val();
		var href = "/api/search/efile_providers?suggestions=true&zipCode=" + zipCode + "&radius=" + radius + "&keywords=" + kw;
		doSearch(href, '#summary');
	}

	function doSearch(href, div) {
		$('#ajax_loading').show();
		$('#details').empty();
		$(div).empty();

		jQuery
				.ajax( {
					type : "GET",
					url : href,
					dataType : "json",
					success : function(summary) {
						$('#ajax_loading').hide();
						$(div).empty();
						$('#suggestions').empty();
						if (summary.suggestions && summary.suggestions.length) {
							$.each(summary.suggestions, function() {
								$('#suggestions').append(this.toString());
							});
						}
						$('#suggestions').show();

						if (summary.docs.length == 0) {
							$(div).append('<h3>No companies found</h3>').show();
						} else {
							var header = '';
							var last_header = '';
							var textToInsert = '';
							var propertyNames = [];
							if (summary.docs && summary.docs.length) {
								$
										.each(
												summary.docs,
												function() {

													if (true || header.length == 0) {
														propertyNames = [];
														for ( var name in this) {
															if (name.match(/latitude/) || name.match(/longitude/) || name.match(/indexDate/)) {
																continue;
															}
															propertyNames
																	.push(name);
														}
														propertyNames
																.sort( function(
																		first,
																		second) {
																	return ((first < second) ? -1
																			: ((first > second) ? 1
																					: 0));
																});
														header = '<tr bgcolor="#DDDDDD">';
														for ( var i = 0; i < propertyNames.length; i++) {
															value = propertyNames[i].toString();
															if (value.length > 200) {
																value = value.substring(0, 200);
															}
															header += '<td>' + value + '</td>';
														}
														header += '</tr>\n';
													}
													if (last_header !== header) {
														textToInsert += header;
														last_header = header;
													}
													textToInsert += '<tr>\n';
													for ( var i = 0; i < propertyNames.length; i++) {
														if (propertyNames[i] == '_id') {
															textToInsert += '<td><a href="#" onclick="show(\'' + this[propertyNames[i]] + '\');">details</a></td>';
														} else if (propertyNames[i] == 'doc') {
															textToInsert += '<td><a href="#" onclick="similar(\'' + this['_id'] + '\',\'' +  this[propertyNames[i]] + '\');">more like this</a></td>\n';
														} else if (this[propertyNames[i]].constructor.toString().indexOf("Array") != -1) {
															textToInsert += '<td>';
															for (var j=0; j<3 && j<this[propertyNames[i]].length; j++) {
																textToInsert += this[propertyNames[i]][j].toString();
															}
															textToInsert += '</td>\n';
														} else {
															textToInsert += '<td>' + this[propertyNames[i]] + '</td>\n';
														}
													}
													textToInsert += '</tr>\n';
												});
							}
							var table = $('<table />\n').attr('cellspacing', 0)
									.attr('cellpadding', 4);
							table.append(textToInsert);
							$(div).append("docs " + summary.docs.length);
							$(div).append(table).show();
						}
					},
					error : function(XMLHttpRequest, textStatus, errorThrown) {
						alert("Error quering");
					}
				});
	}

</script>
</head>
<body>
<form id="search_form" onsubmit="return false;">
<fieldset><label for="keywords"> Keywords: </label> <input
    type="text" id="keywords" class="kw" name="keywords" /> 
<input type="button" id="search" name="search" class="search-cls"
	value="Search" /> <input type="button" id="explain" name="explain"
	class="explain-cls" value="Explain" /> <input type="button" id="top"
	name="top" class="top-cls" value="Top Terms" />
<div id="ajax_loading"><img align="absmiddle"
	src="images/ajax-loader.gif" /> &nbsp;Processing...</div>
</fieldset>
</form>
<table>
	<tr>
		<td valign="top" width="100%" colspan="2">Suggestions:
		<div id="suggestions"></div>
		</td>
	</tr>
	<tr>
		<td valign="top" width="50%">
		<div id="summary"></div>
		</td>
		<td valign="top" width="50%">
		<div id="details"></div>
		</td>
	</tr>
</table>
</body>
</html>
