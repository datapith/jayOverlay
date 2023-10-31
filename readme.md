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

## Overlay document

An overlay document contains an ordered list of Action Objects that are to be applied to the target document. Each Action Object has a target property and a modifier type (update or remove). The target property is a [JSONPath](https://datatracker.ietf.org/wg/jsonpath/documents/) query expression that identifies the elements of the target document to be updated and the modifier determines the change.

Field Name |              Type              | Description
---|:------------------------------:|---
<a name="overlayVersion"></a>overlay |            `string`            | **REQUIRED**. This string MUST be the [version number](#versions) of the Overlay Specification that the Overlay document uses. The `overlay` field SHOULD be used by tooling to interpret the Overlay document.
<a name="overlayInfo"></a>info |   [Info Object](#infoObject)   | **REQUIRED**. Provides metadata about the Overlay. The metadata MAY be used by tooling as required.
<a name="overlayExtends"></a> extends |            `string`            | URL to the target document (such as an OpenAPI document) this overlay applies to. This MUST be in the form of a URL.
<a name="overlayActions"></a>actions | [Action Object](#actionObject) | **REQUIRED** An ordered list of actions to be applied to the target document. The array MUST contain at least one value.

### <a name="infoObject"></a>Info Object

The object provides metadata about the Overlay.
The metadata MAY be used by the clients if needed.

Field Name | Type | Description
---|:---:|---
<a name="infoTitle"></a>title | `string` | **REQUIRED**. A human readable description of the purpose of the overlay.
<a name="infoVersion"></a>version | `string` | **REQUIRED**. A version identifer for indicating changes to the Overlay document.

### <a name="actionObject"></a>Action Object

This object represents one or more changes to be applied to the target document at the location defined by the target JSONPath expression.

Field Name | Type | Description
---|:---:|---
<a name="actionTarget"></a>target | `string` | **REQUIRED** A [JSONPath](https://datatracker.ietf.org/wg/jsonpath/documents/) query expression referencing the target objects in the target document.
<a name="actionDescription"></a>description | `string` | A description of the action. [CommonMark syntax](https://spec.commonmark.org/) MAY be used for rich text representation.
<a name="actionUpdate"></a>update | Any | An object with the properties and values to be merged with the object(s) referenced by the `target`. This property has no impact if `remove` property is `true`.
<a name="actionRemove"></a>remove | `boolean` | A boolean value that indicates that the target object is to be removed from the the map or array it is contained in. The default value is `false`.

The result of the `target` JSONPath query expression must be zero or more objects or arrays (not primitive types or `null` values). If you wish to update a primitive value such as a string, the `target` expression should select the *containing* object in the target document.

The properties of the update object MUST be compatible with the target object referenced by the JSONPath key. When the Overlay document is applied, the properties in the merge object replace properties in the target object with the same name and new properties are appended to the target object.
