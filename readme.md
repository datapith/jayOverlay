# JSON and YAML overlay

Tool to apply overlays to JSON and YAML files. Overlays are a way to extend or enhance an existing JSON/YAML file by adding, updating or removing fields. _**By using Overlays to describe and apply the changes the same changes can instantly be re-applied**_.
 
## How to use

### Gradle
  
Define the overlay you would like to apply, for example the following overlay

```json
{
    "overlay": "0.1.0",
    "info": {
    "title": "Adding description overlay",
    "version": "1.0.0"
},
    "actions": [
        {
            "target": "paths",
            "description": "Adding path to openapi specification",
            "update": {
                "/list" : {
                    "get" : {
                        "description": "Returns a list of stuff",             
                        "responses": {
                            "200": {
                                "description": "Successful response"                                
                            }
                        }
                    }
                }
            }
        }    
    ]
}
```

will add the path `/list` to the following OpenApi spec:

```json
{
   "openapi": "3.0.0",
   "info": {
       "version": "1.0.0",
       "title": "API"
   },
   "paths": {}
}           
```
Add the overlay plugin to your gradle build

```
plugins {
    id("io.datapith.jayOverlay").version("<<latest>>")
}

jayOverlay {
    targetFile.set("api-specs.yaml")
    overlayFile.set("overlay.json")
    outputDir.set("${projectDir}/result"")
}
```
