import models from '../../models';

const removeinternals = (event) => {
  // TODO handler for excess event data (publicity etc.) that we don't want to return
}

exports.allevents = (req, res) => {
  models.event.findAll({
    where: { party: req.params.party } // TODO status = public
  }).then((events) => {
    res.setHeader('Content-Type', 'application/json');
    res.send(200, events);
  }).catch((err) => {
    log.error(new Date(), 'Error when fetching events:', err);
    res.send(500, 'Error when fetching events for party ' + req.params.party);
  });
}

exports.singlepartyevents = (req, res) => {
  if (req.params.party.length < 3) {
    log.error(new Date(), 'Party', req.params.party, 'does not match requirements and cannot exist.');
    res.send(404, 'Defined party does not meet requirements: ' + req.params.party);
  }
  
  models.event.findAll({
    where: { party: req.params.party }
  }).then((events) => {
    res.setHeader('Content-Type', 'application/json');
    res.send(200, events);
  }).catch((err) => {
    log.error(new Date(), 'Error when fetching events for party', req.params.party + ':', err);
    res.send(500, 'Error when fetching events for party ' + req.params.party);
  });
}

exports.taggedevents = (req, res) => {
  if (req.params.party.length < 3 || req.params.tags.length < 2) {
    log.error(new Date(), 'Party', req.params.party, 'or tags', req.params.tags, 'do not match requirements and cannot exist.');
    res.send(404, 'Defined party or tags do not meet requirements: ' + req.params.party);
  }
  
  var tags = [];
  
  if (req.params.tags.includes('+')) {
    req.params.tags.split('+').forEach((tag) => {
      tags.push(tag);
    });
  }
  else {
    tags.push(req.params.tags);
  }
    
  models.event.findAll({
    where: { 
      party: req.params.party,
      tags: { $like: '%' + tags[0] + '%' }
    }
  }).then((events) => {
  
    // If we have more than one tag, we'll filter out events without all of them.
    if (tags.length > 1) {
      var tempevents = [];
      events.forEach((event) => {
        var included = true;
        tags.forEach((tag) => {
          if (!event.tags.includes(tag)) included = false;
        })
        if (included) {
          tempevents.push(event);
        }
      })
      events = tempevents;
    }
  
    res.setHeader('Content-Type', 'application/json');
    res.send(200, events);
  }).catch((err) => {
    log.error(new Date(), 'Error when fetching events for party', req.params.party + ' with tags ' + req.params.tags + ': ' + err);
    res.send(500, 'Error when fetching events for party ' + req.params.party);
  })
}