# Scotbank 

A bank project for CS217 - Agile Software Engineering in Practice

## Running

    ./mvnw.cmd clean jooby:run

## Building

    ./mvnw.cmd clean package

## Testing

    ./mvnw.cmd clean test 

## Interactive Map using Google Maps API
To use the embedded interactive Google Maps view on the manager dashboard, you will need an API key. You can create one
yourself, or ask [Mohammad Rayyan](mailto:mohammad.adhoni.2023@uni.strath.ac.uk) very nicely for one.

Once you have the key, you will need to create an environment variable with the name `API_MAPS_KEY`. This will last for 
your session.

In powershell:
```ps
$env:MAPS_API_KEY="your-key-here"
```
You can test if it's set by using:
```ps
echo $env:MAPS_API_KEY
```

In bash:
```
export MAPS_API_KEY="your-key-here"
```
You can test if it's set by using:
```bash
echo $MAPS_API_KEY
```

## Cloud Migration
Please read our [case for cloud migration](cloudproposal.pdf), where we outline four reasons for a fully-cloud-integrated approach for our app.
