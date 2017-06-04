import models from '../models'
import fs from 'fs'

insertTestdata()


async function insertTestdata() {
  await models.sequelize.sync( { force: true } )
  const users = JSON.parse( fs.readFileSync('testdata/users.json','utf-8') )
  for (const user of users) {
    await models.user.create(user)
  }
  // TODO parties, events, etc 
  models.sequelize.close()
}