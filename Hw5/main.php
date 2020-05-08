<?php
ini_set('memory_limit','-1');
include 'SpellCorrector.php';
// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
$limit = 10;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;
$rank = array('sort' => 'pageRankFile desc' );
if(!isset($_REQUEST['search'])) $_REQUEST['search']='solr';


if ($query)
{
	// The Apache Solr Client library should be on the include path
	// which is usually most easily accomplished by placing in the
	// same directory as this script ( . or current directory is a default
	// php include path entry in the php.ini)
	require_once('Apache/Solr/Service.php');
	// create a new solr service instance - host, port, and corename
	// path (all defaults in this example)
	$solr = new Apache_Solr_Service('localhost', 8983, '/solr/foxnews/');
	// if magic quotes is enabled then stripslashes will be needed
	if (get_magic_quotes_gpc() == 1)
	{
		$query = stripslashes($query);
	}
	// in production code you'll always want to use a try /catch for any
	// possible exceptions emitted by searching (i.e. connection
	// problems or a query parsing error)
	try {
		if ($_REQUEST['search'] == 'solr') {
			$results = $solr->search($query, 0, $limit);
		}else{
			$results = $solr->search($query, 0, $limit, $rank);
		}
	}
	catch (Exception $e)
	{
	// in production you'd probably log or email this error to an admin
	// and then show a special message to the user but for this example
	// we're going to show the full exception
		die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
	}
}
?>
<html>
	<head>
		<title>PHP Solr Client</title>
		<link rel="stylesheet" href="http://code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    	<script src="http://code.jquery.com/jquery-1.10.2.js"></script>
    	<script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

		<script type="text/javascript">
			$(function() {
				$("#q").autocomplete({
					source : function(request, response) {
						var query = $("#q").val().toLowerCase();
						console.log(query);
						$.ajax({
						  	url:  "http://localhost:8983/solr/foxnews/suggest?wt=json&indent=on&q=" + query,
						  	success : function(data) {
				                var res = [];
				                var js =data.suggest.suggester[query].suggestions;
				            	for (var i = 0; i < js.length; i++) {
				            		res[i] = js[i].term;
				            	}
				                console.log(res);
				                response(res);
			            	},
							dataType : 'json'
						});
					},
 					minLength : 1
				})

			});

		</script>

	</head>
<body>

<form accept-charset="utf-8" method="get">
	<table>
		<tr><td>
		<label for="q">Search:</label>
		<input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"   autocomplete="off"/>
		</td></tr>
		<tr><td>
		<input id ="solr" type="radio" name="search" value="solr" <?php if (!isset($_REQUEST['search']) || $_REQUEST['search'] == 'solr') echo "checked"; ?> >
		<label for="solr">Solr Default</label>
		</td></tr>
		<tr><td>
		<input id ="page" type="radio" name="search" value="pageRank" <?php if ($_REQUEST['search'] == 'pageRank') echo "checked"; ?>  >
		<label for="page">PageRank</label>
		</td></tr>
		<tr><td><input type="submit"/></td></tr>
	</table>
</form>

<?php
// display results
	if ($results){
		$total = (int) $results->response->numFound;
		$start = min(1, $total);
		$end = min($limit, $total);


		if ($total == 0) {
?>

	<div>
		<?php 
		 	$new_query=SpellCorrector::correct($query);
			$link = "http://localhost:8080/solr/main.php?q=".$new_query."&search=".$_REQUEST['search'];
      		$output = "Did you mean: <a href='$link'>$new_query</a>";
      		echo $output;
		?>
	</div>

<?php
		}else{
?>
	

	
	<div>
		<p>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</p>

	</div>

	<ol style="text-align: center;">
	<?php
	// iterate result documents
	foreach ($results->response->docs as $doc)
	{
	?>

	<li style="text-align: center;">
	<table style="border: 1px solid black; text-align: left; width: 1000px;">
		
		<?php
		// iterate document fields / values

		$docId = "N/A";
		$docURL = "N/A";
		$docDesc = "N/A";
		$docTitle = "N/A";

		foreach ($doc as $field => $value)
		{
			if ($field == "id") {
				$docId = $value;
			}
			if ($field == "title") {
				$docTitle = $value;
			}
			if ($field == "og_description") {
				$docDesc = $value;
			}
			if ($field == "og_url") {
				$docURL = $value;
			}
		}

		echo "<tr><th>Title:".'<a href='.$docURL.'>'.$docTitle.'</a>'."</th></tr>";
		echo "<tr><td>URL: ".'<a href='.$docURL.' target="_blank">'.$docURL.'</a></td></tr>';
		echo "<tr><td>ID: ".$docId."</td></tr>";			
		echo "<tr><td>Description: ".$docDesc."</td></tr>";
		?>

	</table>
	</li>
	<?php
	}
	?>
	</ol>
	<?php
		}
	}
	?>
</body>
</html>