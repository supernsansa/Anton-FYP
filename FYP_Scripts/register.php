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
$username = htmlspecialchars($_POST['username']);
//Password left unchanged as strong password should contain punctuation
$password = $_POST['password'];

//Check for existing email
$userCheck = mysqli_query($conn, "SELECT * FROM userdb WHERE Email = '$email'");
//If user associated with email already exists, terminate
if (mysqli_num_rows($userCheck,) > 0) {
	die("Email already associated with this account" . $conn->connect_error);
}

//Check for existing username
$userCheck = mysqli_query($conn, "SELECT * FROM userdb WHERE Username = '$username'");
//If user associated with email already exists, terminate
if (mysqli_num_rows($userCheck,) > 0) {
	die("Username already exists" . $conn->connect_error);
}

//Encode and hash password to prevent SQLi and for general security
$encodePass = base64_encode($password);
$hashPass = password_hash($encodePass, PASSWORD_BCRYPT);

//INSERT query 
$userQuery = "INSERT INTO userdb (Username, Email, Password) 
	Values('$username','$email','$hashPass')";

if ($conn->query($userQuery) == TRUE)
{
	echo "Account Created Successfully";
}
else
{
	echo "Account Creation Failed";
}
$conn->close();
?>