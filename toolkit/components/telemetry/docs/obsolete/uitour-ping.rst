
"uitour-tag" ping (obsolete)
============================

Removed in Firefox v140 via bug 1963704.

This ping is submitted via the UITour ``setTreatmentTag`` API. It may be used by
the tour to record what settings were made by a user or to track the result of
A/B experiments.

The client ID and profile group ID are submitted with this ping.

Structure:

.. code-block:: js

    {
      version: 1,
      type: "uitour-tag",
      clientId: <string>,
      profileGroupId: <string>,
      payload: {
        tagName: <string>,
        tagValue: <string>
      }
    }
