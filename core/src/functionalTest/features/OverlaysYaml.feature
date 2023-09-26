Feature: Apply overlays to YAML

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

