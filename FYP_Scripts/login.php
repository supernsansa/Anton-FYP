<?php
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

//Values from form. Sanitized/filtered to prevent SQLi
$email = filter_var(($_POST['email']),FILTER_SANITIZE_EMAIL); 
//Password left unchanged as strong password should contain punctuation
$password = $_POST['password'];
$username = "null";

//Query
$userQuery = "SELECT * FROM userdb WHERE Email = '$email'";
$userResult = $conn->query($userQuery);

if ($userResult->num_rows > 0) 
{
	$userRow = $userResult->fetch_assoc();
	//If password is correct
	if (password_verify(base64_encode($password), $userRow['Password'])) 
	{
		//Login success
		$username = $userRow['Username'];
		echo $username;
	}
	else 
	{
		echo "Login Failure";
	}
}
else 
	{
		echo "Login Failure";
	}
$conn->close();
?>