crear venv python -m venv venv
(windows) venv\Scripts\activate
(unix) source venv/Scripts/activate
para generar secret key python -c 'import secrets; print(secrets.token_urlsafe(50))'