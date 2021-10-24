<?php
//This script edits the definition of a word
//Server and DB connection values
$servername = "localhost";
$rootUser = "root";
$db = "bsl4kids";
$rootPassword = "";

//Create connection
$conn = new mysqli($servername, $rootUser, $rootPassword, $db);

//Check connection
if ($conn->connect_error)
	{
		die("Connection failed: " . $conn->connect_error);
	}

//Get word and word description from app
$word_name = $_POST["wordName"];
$word_desc = base64_encode($_POST["wordDesc"]);

$user = 1; //TODO: Change this

//Query to worddb
$wordQuery = "UPDATE worddb SET Definition = '$word_desc' WHERE WordName = '$word_name'";
//Test if query is successful
if ($conn->query($wordQuery) == TRUE)
{
  echo "Success! The word/phrase has been edited";
}
else
{
  echo "Hmm, some error seems to have occurred...";
}
$conn->close();
?>