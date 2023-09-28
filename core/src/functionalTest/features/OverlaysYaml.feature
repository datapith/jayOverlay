Feature: Apply overlays to YAML

  Scenario: overlay should add body to all endpoints
    Given a 'YAML' document
    """
    openapi: 3.0.0
    info:
      title: "Example JayOverlay"
      version: 1.0.0
    paths:
      /test:
        post:
          requestBody:
            required: true
          responses:
            '200':
              description: "Success"
            '400':
              description: "Invalid request"
            '500':
              description: "Internal Error"
    """
    And a overlay
    """
    {
      "overlay": "0.1.0",
      "info": {
          "title": "Adding Body",
          "version": "1.0.0"
      },
      "extends": "<<target-document-location>>",
      "actions": [
          {
            "target": "paths.\"/test\".post.requestBody",
            "update": {
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "required": [
                      "firstName",
                      "lastName"
                    ],
                    "properties": {
                      "firstName": {
                        "type": "string",
                        "nullable": false
                      },
                      "lastName": {
                        "type": "string",
                        "nullable": false
                      }
                    }
                  }
                }
              }
            }
          }
      ]
    }
    """
    When applying overlay to document
    Then updates are applied to document
    """
    openapi: "3.0.0"
    info:
      title: "Example JayOverlay"
      version: "1.0.0"
    paths:
      /test:
        post:
          requestBody:
            required: true
            content:
              application/json:
                schema:
                  type: "object"
                  required:
                  - "firstName"
                  - "lastName"
                  properties:
                    firstName:
                      type: "string"
                      nullable: false
                    lastName:
                      type: "string"
                      nullable: false
          responses:
            "200":
              description: "Success"
            "400":
              description: "Invalid request"
            "500":
              description: "Internal Error"
    """

  Scenario: overlay should add global header parameter
    Given a 'YAML' document
    """
    swagger: '2.0'
    info:
      title: "Example JayOverlay"
      version: "1.0.0"
    parameters:
     CommonPathParameterHeader:
       name: "COMMON-PARAMETER-HEADER"
       type: "string"
       in: "header"
       required: true
    paths:
      /firstEndpoint:
        get:
          responses:
            "200":
              description: "Success"
            "400":
              description: "Invalid request"
            "500":
              description: "Internal Error"
      /secondEndpoint:
        get:
          responses:
            "200":
              description: "Success"
            "400":
              description: "Invalid request"
            "500":
              description: "Internal Error"
    """
    And a overlay
    """
    {
      "overlay": "0.1.0",
      "info": {
          "title": "Adding Body",
          "version": "1.0.0"
      },
      "extends": "<<target-document-location>>",
      "actions": [
          {
              "target": "paths.*",
              "update": {
                "parameters":[
                  {
                    "$ref":"#/parameters/CommonPathParameterHeader"
                  }
                ]
              }
          }
      ]
    }
    """
    When applying overlay to document
    Then updates are applied to document
    """
    swagger: '2.0'
    info:
      title: "Example JayOverlay"
      version: "1.0.0"
    parameters:
     CommonPathParameterHeader:
       name: "COMMON-PARAMETER-HEADER"
       type: "string"
       in: "header"
       required: true
    paths:
      /firstEndpoint:
        get:
          responses:
            "200":
              description: "Success"
            "400":
              description: "Invalid request"
            "500":
              description: "Internal Error"
        parameters:
          - $ref: "#/parameters/CommonPathParameterHeader"
      /secondEndpoint:
        get:
          responses:
            "200":
              description: "Success"
            "400":
              description: "Invalid request"
            "500":
              description: "Internal Error"
        parameters:
          - $ref: "#/parameters/CommonPathParameterHeader"
    """

  Scenario: overlay should add multiple Nodes to yaml file
    Given a 'YAML' document
    """
    openapi: "3.0.0"
    info:
      version: "1.0.0"
      title: "API"
    paths: {}            
    """
    And a overlay
    """
    {
        "overlay": "0.1.0",
        "info": {
            "title": "Adding information overlay",
            "version": "1.0.0"
        },
        "extends": "<<target-document-location>>",
        "actions": [
            {
                "target": "info",
                "description": "Adding extra information to openapi specification",
                "update": {
                    "description": "Added description",
                    "termsOfService": "http://example.com/terms/"
                }
            }
        ]
    }
    """
    When applying overlay to document
    Then updates are applied to document
    """
    openapi: "3.0.0"
    info:
      version: "1.0.0"
      title: "API"
      description: "Added description"
      "termsOfService": "http://example.com/terms/"
    paths: {}
    """

