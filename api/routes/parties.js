import models from '../../models';

const removeinternals = (event) => {
    // TODO handler for excess party data (publicity etc.) that we don't want to return
  }

exports.allparties = (req, res) => {
  models.party.findAll({
    where: { active: true }
  }).then((parties) => {
    res.send(200, parties);
  }).catch((err) => {
    log.error(new Date() + ' Error when fetching parties: ' + err);
    res.send(500, 'Error when fetching parties. Please check service status.');
  });
}

exports.singleparty = (req, res) => {
  if (req.params.partyid.length < 3) {
    log.error(new Date(), 'Party', req.params.party, 'does not match requirements and cannot exist.');
    res.send(404, 'Defined party does not meet requirements: ' + req.params.party);
  }
  
  models.party.findOne({
    where: { 
      shortname: req.params.partyid,
      active: true
    }
  }).then((singleparty) => {
    // const cleanedparty = removeinternals(singleparty);
    res.send(200, singleparty);
  }).catch((err) => {
    log.error(new Date() + ' Error when fetching parties: ' + err);
    res.send(500, 'Error when fetching party. Please check service status.');
  })
}