<?php
/*
 *  
 *  Mendroid: a Mendeley Android client
 *  Copyright 2011 Martin Paul Eve <martin@martineve.com>
 *
 *  This file is part of Mendroid.
 *
 *  Mendroid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *   
 *  Mendroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Mendroid.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
if($_GET['oauth_verifier'] != "" && $_GET['acc_name'] != "") {
   header( 'Location: mendeley-authcallback:///?acc_name=' . urlencode($_GET['acc_name']) .'&oauth_verifier=' . urlencode($_GET['oauth_verifier']) ) ;
}
else
{
   header( 'Location: mendeley-authfail:///?acc_name=' . urlencode($_GET['acc_name'])) ;
}
?>
