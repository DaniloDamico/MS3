import React from "react";
import { Link } from "react-router-dom";
import {
  Dropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
  Collapse,
  NavItem,
  NavLink
} from "shards-react";
import {UtenteAPI} from "../../../../API/UtenteAPI";

export default class UserActions extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      visible: false,
      nome : "",
      cognome:"",
    };

    this.toggleUserActions = this.toggleUserActions.bind(this);
  }

  async componentDidMount() {
    let utente = await(new UtenteAPI().getUserDetails(7));
    this.setState({
      nome: utente.nome,
      cognome : utente.cognome
    })
  }

    toggleUserActions() {
    this.setState({
      visible: !this.state.visible
    });
  }

  render() {
    return (
      <NavItem tag={Dropdown} caret toggle={this.toggleUserActions}>
        <DropdownToggle caret tag={NavLink} className="text-nowrap px-3">

          <span className="d-none d-md-inline-block">{this.state.nome+" "+this.state.cognome}</span>
        </DropdownToggle>
        <Collapse tag={DropdownMenu} right small open={this.state.visible}>
          <DropdownItem tag={Link} to="/user-profile">
            <i className="material-icons">&#xE7FD;</i> Profilo
          </DropdownItem>
          <DropdownItem tag={Link} to="edit-user-profile">
            <i className="material-icons">&#xE8B8;</i> Modifica Preferenze
          </DropdownItem>
          <DropdownItem tag={Link} to="/" className="text-danger">
            <i className="material-icons text-danger">&#xE879;</i> Logout
          </DropdownItem>
        </Collapse>
      </NavItem>
    );
  }
}
